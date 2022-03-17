package io.onduktor.demos.kafka;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

public class ConsumerDemoWithShutdown {

    private static final Logger log = LoggerFactory.getLogger(ConsumerDemoWithShutdown.class.getSimpleName()); // logger from this class
    public static void main(String[] args) {
        log.info("I am a kafka consumer");
        String bootstrapsServer = "127.0.0.1:9092";
        String groupId = "mythird-app";
        String topic = "demo_java";


        //create consumer config
        Properties properties = new Properties();
        properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,bootstrapsServer);
        properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        // create consumer
        KafkaConsumer<String,String> consumer = new KafkaConsumer<>(properties);


        //get a reference to the current thread

        final   Thread mainThread = Thread.currentThread();


        // adding shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Detected shutdown, let's exit by calling consumer.wakeup ....");
            consumer.wakeup();

            //join the main thread to allow the execution of the code in the main thread

            try {
                mainThread.join();
            }catch (InterruptedException e){
                e.printStackTrace();
            }
        }));


        try{
            // subscribe consumer to topic(s)
            // for single
            consumer.subscribe(Collections.singleton(topic));

            // for array
//        consumer.subscribe(Arrays.asList(topic));

            // poll for new data
            while(true){

                log.info("polling");
                // wait until 100 ms to get some records else continue // records is empty
                ConsumerRecords<String,String> records = consumer.poll(Duration.ofMillis(100));

                for (ConsumerRecord<String,String > record:records){
                    log.info("Key: " +record.key() + "Value: " +record.value());
                    log.info("Partition: " +record.partition() + "Offset: " +record.offset());
                }
            }
        }catch (WakeupException e){
            log.info("wake up exception");
            //we ignore this as this is an expected exception when closing a consumer

        }catch (Exception e){
            log.error("unexpected exception");
        }finally {
            consumer.close(); // this will also commit the offsets if need be
            log.info(" the consumer gracefully shutdown");
        }

    }
}
