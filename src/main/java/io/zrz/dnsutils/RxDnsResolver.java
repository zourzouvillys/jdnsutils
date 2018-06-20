package io.zrz.dnsutils;

import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.handler.codec.dns.DefaultDnsQuestion;
import io.netty.handler.codec.dns.DnsQuestion;
import io.netty.handler.codec.dns.DnsRecordType;
import io.netty.handler.codec.dns.DnsResponse;
import io.netty.resolver.dns.DnsNameResolver;
import io.reactivex.Single;

public class RxDnsResolver {

  private static final Logger log = LoggerFactory.getLogger(RxDnsResolver.class);
  private final DnsNameResolver resolver;

  public RxDnsResolver(final DnsNameResolver resolver) {
    this.resolver = resolver;
  }

  /**
   * send a question, returns answer or error.
   *
   * @param question
   * @return
   */

  public Single<DnsResponse> send(final DnsQuestion question) {
    log.debug("looking up SRV records for {}", question);
    return NettyFutureAdapter.toSingle(this.resolver.query(question))
        .map(x -> {
          final DnsResponse res = x.content().retain();
          x.release();
          return res;
        })
        .doOnSuccess(res -> log.debug("DNS response: {}", res));
  }

  /**
   * attempts to resolve the given SRV record.
   */

  public Single<List<SrvRecord>> resolveSRV(final String service, final String proto, final String name) {

    Objects.requireNonNull(service);
    Objects.requireNonNull(proto);
    Objects.requireNonNull(name);

    final String fqdn = "_" + service + "._" + proto + "." + name;

    return this
        .send(new DefaultDnsQuestion(fqdn, DnsRecordType.SRV))
        .map(DnsSrvRecords::parse);

  }

  /**
   *
   */

  public static RxDnsResolver of(final DnsNameResolver resolver) {
    return new RxDnsResolver(resolver);
  }

}
