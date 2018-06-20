package io.zrz.dnsutils;

import java.util.concurrent.TimeUnit;

import org.reactivestreams.Subscriber;

import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.exceptions.Exceptions;
import io.reactivex.internal.subscriptions.DeferredScalarSubscription;

class NettyFutureAdapter {

  public static <T, F extends Future<T>> GenericFutureListener<F> mappedTo(final SingleEmitter<? super T> emitter) {
    return future -> {
      if (future.isSuccess()) {
        emitter.onSuccess(future.get());
      }
      else {
        emitter.onError(future.cause());
      }
    };
  }

  // -----

  public static final class FlowableFromFuture<T> extends Flowable<T> {

    final Future<? extends T> future;
    final long timeout;
    final TimeUnit unit;

    public FlowableFromFuture(final Future<? extends T> future, final long timeout, final TimeUnit unit) {
      this.future = future;
      this.timeout = timeout;
      this.unit = unit;
    }

    @Override
    public void subscribeActual(final Subscriber<? super T> s) {

      final DeferredScalarSubscription<T> deferred = new DeferredScalarSubscription<>(s);
      s.onSubscribe(deferred);

      T v;

      try {
        v = this.unit != null ? this.future.get(this.timeout, this.unit) : this.future.get();
      }
      catch (final Throwable ex) {
        Exceptions.throwIfFatal(ex);
        if (!deferred.isCancelled()) {
          s.onError(ex);
        }
        return;
      }

      if (v == null) {
        s.onError(new NullPointerException("The future returned null"));
      }
      else {
        deferred.complete(v);
      }

    }

  }

  public static <T> Single<T> toSingle(final Future<T> future) {

    return Single.create(emitter ->

    future.addListener(future1 -> {
      if (future1.isSuccess()) {
        emitter.onSuccess((T) future1.get());
      }
      else {
        emitter.onError(future1.cause());
      }
    })

    );

  }

}
