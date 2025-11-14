package com.innowise.payment.util;

import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

public class TestKafkaConsumer implements AutoCloseable {

  private final KafkaConsumer<String, String> consumer;

  public TestKafkaConsumer(String bootstrapServers, String topic) {
    Properties props = new Properties();
    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    props.put(ConsumerConfig.GROUP_ID_CONFIG, "test-consumer");
    props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());

    this.consumer = new KafkaConsumer<>(props);
    this.consumer.subscribe(Collections.singletonList(topic));
  }

  public ConsumerRecord<String, String> pollRecord(long timeout, TimeUnit unit) {
    ConsumerRecords<String, String> records = consumer.poll(unit.toMillis(timeout));
    if (records.isEmpty()) {
      return null;
    }
    return records.iterator().next();
  }

  @Override
  public void close() {
    consumer.close();
  }
}