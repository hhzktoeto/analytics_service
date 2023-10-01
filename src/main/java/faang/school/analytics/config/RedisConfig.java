package faang.school.analytics.config;

import faang.school.analytics.listener.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@RequiredArgsConstructor
public class RedisConfig {

    @Value("${spring.data.redis.host}")
    private String host;

    @Value("${spring.data.redis.port}")
    private int port;

    @Value("${spring.data.redis.channels.recommendation_channel}")
    private String recommendationChannelName;

    @Value("${spring.data.redis.channels.follower_channel}")
    private String followerChannelName;

    @Value("${spring.data.redis.channels.comment_event_channel}")
    private String commentEventChannelName;

    @Value("${spring.data.redis.channels.like_channel}")
    private String likePostChannelName;

    @Bean
    public MessageListenerAdapter recommendationEventAdapter(RecommendationEventListener listener) {
        return new MessageListenerAdapter(listener);
    }

    @Bean
    public MessageListenerAdapter followerEventAdapter(FollowerEventListener listener) {
        return new MessageListenerAdapter(listener);
    }

    @Bean
    public MessageListenerAdapter commentEventAdapter(CommentEventListener listener) {
        return new MessageListenerAdapter(listener);
    }

    @Bean
    public MessageListenerAdapter likePostEventAdapter(LikePostEventListener listener) {
        return new MessageListenerAdapter(listener);
    }

    @Value("${spring.data.redis.channels.mentorship_request_chanel}")
    private String mentorshipRequestChannelName;

    private final RecommendationEventListener recommendationEventListener;

    @Bean
    public JedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(host, port);
        return new JedisConnectionFactory(config);
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        return redisTemplate;
    }

    @Bean
    RedisMessageListenerContainer redisContainer(MessageListenerAdapter recommendationEventAdapter,
                                                 MessageListenerAdapter commentEventAdapter,
                                                 MessageListenerAdapter followerEventAdapter,
                                                 MessageListenerAdapter mentorshipRequestListener,
                                                 MessageListenerAdapter likePostEventAdapter) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(redisConnectionFactory());
        container.addMessageListener(recommendationEventListener, topicRecommendation());
        container.addMessageListener(mentorshipRequestListener, topicMentorshipRequest());
        container.addMessageListener(recommendationEventAdapter, topicRecommendation());
        container.addMessageListener(followerEventAdapter, topicFollower());
        container.addMessageListener(commentEventAdapter, topicCommentEvent());
        container.addMessageListener(likePostEventAdapter, topicLikePost());
        return container;
    }

    @Bean
    ChannelTopic topicRecommendation() {
        return new ChannelTopic(recommendationChannelName);
    }

    @Bean
    ChannelTopic topicMentorshipRequest() {
        return new ChannelTopic(mentorshipRequestChannelName);
    }

    @Bean
    MessageListenerAdapter mentorshipRequestListener(MentorshipRequestedEventListener mentorshipRequestedEventListener) {
        return new MessageListenerAdapter(mentorshipRequestedEventListener);
    }

    @Bean
    ChannelTopic topicFollower() {
        return new ChannelTopic(followerChannelName);
    }

    @Bean
    ChannelTopic topicCommentEvent() {
        return new ChannelTopic(commentEventChannelName);
    }

    @Bean
    ChannelTopic topicLikePost() {
        return new ChannelTopic(likePostChannelName);
    }
}