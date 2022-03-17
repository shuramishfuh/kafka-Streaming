package io.onduktor.demos.kafka;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class ProducerDemoKeys {

    private static final Logger log = LoggerFactory.getLogger(ProducerDemoKeys.class.getSimpleName()); // logger from this class
    public static void main(String[] args) {
        log.info("I am a kafka producer ");


        // create producer properties
        Properties properties = new Properties();
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,"127.0.0.1:9092");
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,StringSerializer.class.getName());

        // create the producer
        KafkaProducer<String, String> producer =new KafkaProducer<>(properties);


        for (int i=0; i<10 ;i++){
            String topic ="demo_java";
            String value =" product";
            String key ="id_"+i;
            // create a producer record
            ProducerRecord<String,String> producerRecord = new ProducerRecord<>(topic,key,value );

            //send data -async
            producer.send(producerRecord, (metadata, exception) -> {
                // executes everytime a record is successfully sent of there is an exception
                if(exception == null){
                    //sent successfully
                    log.info("Received new metadata/ \n" +
                            "Topic: " + metadata.topic() + "\n" +
                             "Key: " + producerRecord.key() + "\n" +
                            "Partition: " +metadata.partition() + "\n" +
                            "Offset: " +metadata.offset() + "\n" +
                            "Time: " +metadata.timestamp());
                }
                else{
                    log.error("error while producing ", exception);
                }
            });

        }


        // flush  -sync
        producer.flush();

        // flush and close
        producer.close();

    }
}
