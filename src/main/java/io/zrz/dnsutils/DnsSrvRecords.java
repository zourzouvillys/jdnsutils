package io.zrz.dnsutils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.dns.DefaultDnsRawRecord;
import io.netty.handler.codec.dns.DefaultDnsRecordDecoder;
import io.netty.handler.codec.dns.DnsOpCode;
import io.netty.handler.codec.dns.DnsRawRecord;
import io.netty.handler.codec.dns.DnsRecordType;
import io.netty.handler.codec.dns.DnsResponse;
import io.netty.handler.codec.dns.DnsResponseCode;
import io.netty.handler.codec.dns.DnsSection;

public class DnsSrvRecords {

  public static List<SrvRecord> parse(final DnsResponse res) {

    try {

      if (res.opCode() != DnsOpCode.QUERY) {
        throw new IllegalArgumentException(res.opCode().toString());
      }

      if (res.code() != DnsResponseCode.NOERROR) {
        // we currently don't differentiate between no records and NXdomain etx. it doesn't normally
        // make any difference ot the consumer...
        return Collections.emptyList();
      }

      // System.err.println(res.count());

      final List<SrvRecord> records = IntStream.range(0, res.count(DnsSection.ANSWER))
          .mapToObj(idx -> (DefaultDnsRawRecord) res.recordAt(DnsSection.ANSWER, idx))
          .filter(e -> e.type() != DnsRecordType.OPT)
          .map(c -> decodeRecord(c))
          .collect(Collectors.toList());

      // System.err.println(res.count(DnsSection.AUTHORITY));

      // IntStream.range(0, res.count(DnsSection.AUTHORITY))
      // .mapToObj(idx -> (DefaultDnsRawRecord) res.recordAt(DnsSection.AUTHORITY, idx))
      // .filter(e -> e.type() != DnsRecordType.OPT)
      // .map(c -> decodeRecord(c))
      // .collect(Collectors.toList());
      //
      // final List<SrvRecord> additional = IntStream
      // .range(0, res.count(DnsSection.ADDITIONAL))
      // .mapToObj(idx -> (DefaultDnsRawRecord) res.recordAt(DnsSection.ADDITIONAL, idx))
      // .filter(e -> e.type() != DnsRecordType.OPT)
      // .map(c -> decodeRecord(c))
      // .collect(Collectors.toList());

      return records;

    }
    finally {

      res.release();

    }

  }

  public static SrvRecord decodeRecord(final DnsRawRecord in) {

    // System.err.println(in.toString());
    // System.err.println(in.getClass());
    // System.err.println(in.dnsClass());
    // System.err.println(in.type());
    // System.err.println(in.name());

    final ByteBuf content = in.content();

    final int priority = content.readUnsignedShort();
    final int weight = content.readUnsignedShort();
    final int port = content.readUnsignedShort();

    final String name = DefaultDnsRecordDecoder.decodeName(content);

    return new SrvRecord(in, priority, weight, port, name);

  }

}
