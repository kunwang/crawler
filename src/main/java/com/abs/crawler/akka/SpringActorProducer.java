package com.abs.crawler.akka;

import akka.actor.IndirectActorProducer;
import org.springframework.context.ApplicationContext;

/**
 * Spring indirect actor producer
 * source: https://github.com/typesafehub/activator-akka-java-spring
 */
public class SpringActorProducer implements IndirectActorProducer {
	final ApplicationContext applicationContext;
	final String actorBeanName;
	final Class<?> requiredType;

	public SpringActorProducer(ApplicationContext applicationContext, String actorBeanName) {
		this(applicationContext, actorBeanName, null);
	}

	public SpringActorProducer(ApplicationContext applicationContext, Class<?> requiredType) {
		this(applicationContext, null, requiredType);
	}

	public SpringActorProducer(ApplicationContext applicationContext,
							   String actorBeanName, Class<?> requiredType) {
		this.applicationContext = applicationContext;
		this.actorBeanName = actorBeanName;
		this.requiredType = requiredType;
	}

	public akka.actor.Actor produce() {
		akka.actor.Actor result;
		if (actorBeanName != null && requiredType != null) {
			result = (akka.actor.Actor) applicationContext.getBean(actorBeanName, requiredType);
		} else if (requiredType != null) {
			result = (akka.actor.Actor) applicationContext.getBean(requiredType);
		} else {
			result = (akka.actor.Actor) applicationContext.getBean(actorBeanName);
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	public Class<? extends akka.actor.Actor> actorClass() {
		return (Class<? extends akka.actor.Actor>) (requiredType != null ? requiredType : applicationContext.getType(actorBeanName));
	}
}
