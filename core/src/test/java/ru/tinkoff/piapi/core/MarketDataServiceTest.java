package ru.tinkoff.piapi.core;

import com.google.protobuf.Timestamp;
import io.grpc.Channel;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.Test;
import ru.tinkoff.piapi.contract.v1.CandleInterval;
import ru.tinkoff.piapi.contract.v1.GetCandlesRequest;
import ru.tinkoff.piapi.contract.v1.GetCandlesResponse;
import ru.tinkoff.piapi.contract.v1.GetLastPricesRequest;
import ru.tinkoff.piapi.contract.v1.GetLastPricesResponse;
import ru.tinkoff.piapi.contract.v1.GetOrderBookRequest;
import ru.tinkoff.piapi.contract.v1.GetOrderBookResponse;
import ru.tinkoff.piapi.contract.v1.GetTradingStatusRequest;
import ru.tinkoff.piapi.contract.v1.GetTradingStatusResponse;
import ru.tinkoff.piapi.contract.v1.HistoricCandle;
import ru.tinkoff.piapi.contract.v1.LastPrice;
import ru.tinkoff.piapi.contract.v1.MarketDataServiceGrpc;
import ru.tinkoff.piapi.contract.v1.MarketDataStreamServiceGrpc;
import ru.tinkoff.piapi.core.utils.DateUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.mockito.AdditionalAnswers.delegatesTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class MarketDataServiceTest extends GrpcClientTester<MarketDataService> {

  @Override
  protected MarketDataService createClient(Channel channel) {
    return new MarketDataService(
      MarketDataServiceGrpc.newBlockingStub(channel),
      MarketDataServiceGrpc.newStub(channel));
  }

  @Test
  void getCandles_Test() {
    var expected = GetCandlesResponse.newBuilder()
      .addCandles(HistoricCandle.newBuilder().setVolume(1).build())
      .build();
    var grpcService = mock(MarketDataServiceGrpc.MarketDataServiceImplBase.class, delegatesTo(
      new MarketDataServiceGrpc.MarketDataServiceImplBase() {
        @Override
        public void getCandles(GetCandlesRequest request,
                               StreamObserver<GetCandlesResponse> responseObserver) {
          responseObserver.onNext(expected);
          responseObserver.onCompleted();
        }
      }));
    var service = mkClientBasedOnServer(grpcService);

    var inArg = GetCandlesRequest.newBuilder()
      .setFigi("figi")
      .setFrom(Timestamp.newBuilder().setSeconds(1234567890).build())
      .setTo(Timestamp.newBuilder().setSeconds(1234567890).setNanos(111222333).build())
      .setInterval(CandleInterval.CANDLE_INTERVAL_1_MIN)
      .build();
    var actualSync = service.getCandlesSync(inArg.getFigi(), DateUtils.timestampToInstant(inArg.getFrom()),
      DateUtils.timestampToInstant(inArg.getTo()), inArg.getInterval());
    var actualAsync = service.getCandles(inArg.getFigi(), DateUtils.timestampToInstant(inArg.getFrom()),
      DateUtils.timestampToInstant(inArg.getTo()), inArg.getInterval()).join();

    assertIterableEquals(expected.getCandlesList(), actualSync);
    assertIterableEquals(expected.getCandlesList(), actualAsync);

    verify(grpcService, times(2)).getCandles(eq(inArg), any());
  }

  @Test
  void getLastPrices_Test() {
    var expected = GetLastPricesResponse.newBuilder()
      .addLastPrices(LastPrice.newBuilder().setFigi("figi1").build())
      .addLastPrices(LastPrice.newBuilder().setFigi("figi2").build())
      .build();
    var grpcService = mock(MarketDataServiceGrpc.MarketDataServiceImplBase.class, delegatesTo(
      new MarketDataServiceGrpc.MarketDataServiceImplBase() {
        @Override
        public void getLastPrices(GetLastPricesRequest request,
                                  StreamObserver<GetLastPricesResponse> responseObserver) {
          responseObserver.onNext(expected);
          responseObserver.onCompleted();
        }
      }));
    var service = mkClientBasedOnServer(grpcService);

    var inArg = GetLastPricesRequest.newBuilder()
      .addFigi("figi1")
      .addFigi("figi2")
      .build();
    var actualSync = service.getLastPricesSync(List.of("figi1", "figi2"));
    var actualAsync = service.getLastPrices(List.of("figi1", "figi2")).join();

    assertIterableEquals(expected.getLastPricesList(), actualSync);
    assertIterableEquals(expected.getLastPricesList(), actualAsync);

    verify(grpcService, times(2)).getLastPrices(eq(inArg), any());
  }

  @Test
  void getOrderBook_Test() {
    var expected = GetOrderBookResponse.newBuilder()
      .setFigi("figi")
      .setDepth(10)
      .build();
    var grpcService = mock(MarketDataServiceGrpc.MarketDataServiceImplBase.class, delegatesTo(
      new MarketDataServiceGrpc.MarketDataServiceImplBase() {
        @Override
        public void getOrderBook(GetOrderBookRequest request,
                                 StreamObserver<GetOrderBookResponse> responseObserver) {
          responseObserver.onNext(expected);
          responseObserver.onCompleted();
        }
      }));
    var service = mkClientBasedOnServer(grpcService);

    var inArg = GetOrderBookRequest.newBuilder()
      .setFigi(expected.getFigi())
      .setDepth(expected.getDepth())
      .build();
    var actualSync = service.getOrderBookSync(inArg.getFigi(), inArg.getDepth());
    var actualAsync = service.getOrderBook(inArg.getFigi(), inArg.getDepth()).join();

    assertEquals(expected, actualSync);
    assertEquals(expected, actualAsync);

    verify(grpcService, times(2)).getOrderBook(eq(inArg), any());
  }

  @Test
  void getTradingStatus_Test() {
    var expected = GetTradingStatusResponse.newBuilder()
      .setFigi("figi")
      .build();
    var grpcService = mock(MarketDataServiceGrpc.MarketDataServiceImplBase.class, delegatesTo(
      new MarketDataServiceGrpc.MarketDataServiceImplBase() {
        @Override
        public void getTradingStatus(GetTradingStatusRequest request,
                                     StreamObserver<GetTradingStatusResponse> responseObserver) {
          responseObserver.onNext(expected);
          responseObserver.onCompleted();
        }
      }));
    var service = mkClientBasedOnServer(grpcService);

    var inArg = GetTradingStatusRequest.newBuilder()
      .setFigi(expected.getFigi())
      .build();
    var actualSync = service.getTradingStatusSync(inArg.getFigi());
    var actualAsync = service.getTradingStatus(inArg.getFigi()).join();

    assertEquals(expected, actualSync);
    assertEquals(expected, actualAsync);

    verify(grpcService, times(2)).getTradingStatus(eq(inArg), any());
  }

}
