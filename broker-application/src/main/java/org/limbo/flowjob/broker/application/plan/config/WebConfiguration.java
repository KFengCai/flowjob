package org.limbo.flowjob.broker.application.plan.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import lombok.extern.slf4j.Slf4j;
import org.limbo.flowjob.common.utils.time.Formatters;
import org.springframework.context.annotation.Bean;
import org.springframework.format.FormatterRegistry;
import org.springframework.format.datetime.DateFormatter;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @author Devil
 * @since 2021/7/26
 */
@Slf4j
public class WebConfiguration implements WebMvcConfigurer {

    /**
     * json 返回结果处理
     */
    @Bean
    public ObjectMapper jacksonObjectMapper() {
        JavaTimeModule module = new JavaTimeModule();
        LocalDateTimeDeserializer dateTimeDeserializer = new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern(Formatters.YMD_HMS));
        module.addDeserializer(LocalDateTime.class, dateTimeDeserializer);
        ObjectMapper objectMapper = Jackson2ObjectMapperBuilder.json().modules(module)
                .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS).build();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        return objectMapper;
    }

//    @Bean
//    @Primary
//    @ConditionalOnMissingBean(ObjectMapper.class)
//    public ObjectMapper jacksonObjectMapper(Jackson2ObjectMapperBuilder builder) {
//        ObjectMapper objectMapper = builder.createXmlMapper(false).build();
//        // 通过该方法对mapper对象进行设置，所有序列化的对象都将按改规则进行系列化
//        // Include.Include.ALWAYS 默认
//        // Include.NON_DEFAULT 属性为默认值不序列化
//        // Include.NON_EMPTY 属性为 空（""） 或者为 NULL 都不序列化，则返回的json是没有这个字段的。这样对移动端会更省流量
//        // Include.NON_NULL 属性为NULL 不序列化,就是为null的字段不参加序列化
//        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
//        objectMapper.setDateFormat(new SimpleDateFormat(TIME_PATTERN));
////        objectMapper.setTimeZone( GMT+8 ) 时区偏移设置，如果不指定的话时间和北京时间会差八个小时
//        return objectMapper;
//    }

//    @Bean
//    public ObjectMapper serializingObjectMapper() {
//        JavaTimeModule module = new JavaTimeModule();
//        LocalDateTimeDeserializer dateTimeDeserializer = new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern(TIME_PATTERN));
//        module.addDeserializer(LocalDateTime.class, dateTimeDeserializer);
//        return Jackson2ObjectMapperBuilder.json().modules(module)
//                .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS).build();
//    }

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addFormatter(new DateFormatter(Formatters.YMD_HMS));
    }

// todo ??? work调用先不用拦截 ak/sk
    //
//    /**
//     * worker 会话拦截
//     *
//     * @return
//     */
//    @Bean
//    public WorkerInterceptor workerInterceptor() {
//        return new WorkerInterceptor();
//    }
//
//    @Override
//    public void addInterceptors(InterceptorRegistry registry) {
//        registry.addInterceptor(workerInterceptor())
//                .addPathPatterns("/api/v1/rpc/worker/*/heartbeat")
//                .addPathPatterns("/api/v1/rpc/worker/task/*/feedback")
////                .excludePathPatterns("/api/v1/rpc/worker")
//        ;
//    }
}
