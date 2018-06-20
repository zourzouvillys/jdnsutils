package io.zrz.dnsutils;

import io.netty.handler.codec.dns.AbstractDnsRecord;
import io.netty.handler.codec.dns.DnsRawRecord;
import io.netty.handler.codec.dns.DnsRecordType;
import io.netty.util.internal.StringUtil;

public class SrvRecord extends AbstractDnsRecord {

  SrvRecord(final DnsRawRecord in, final int priority, final int weight, final int port, final String name) {
    super(in.name(), DnsRecordType.SRV, in.dnsClass(), in.timeToLive());
    this.priority = priority;
    this.weight = weight;
    this.port = port;
    this.name = name;
  }

  private final int priority;
  private final int port;
  private final int weight;
  private final String name;

  public int priority() {
    return this.priority;
  }

  public int port() {
    return this.port;
  }

  public int weight() {
    return this.weight;
  }

  @Override
  public String name() {
    return this.name;
  }

  @Override
  public String toString() {

    final StringBuilder buf = new StringBuilder(64).append(StringUtil.simpleClassName(this)).append('(');

    buf.append(this.name().isEmpty() ? "<root>" : this.name())
        .append(' ')
        .append(this.timeToLive())
        .append(' ');

    buf.append(this.dnsClass());
    buf.append(" ");
    buf.append(this.type());
    buf.append(" ");
    buf.append(this.priority);
    buf.append(" ");
    buf.append(this.weight);
    buf.append(" ");
    buf.append(this.port);
    buf.append(" ");
    buf.append(this.name);
    buf.append(")");

    return buf.toString();
  }

}
