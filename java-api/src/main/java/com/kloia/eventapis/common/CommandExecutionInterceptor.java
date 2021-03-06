package com.kloia.eventapis.common;


import com.kloia.eventapis.kafka.KafkaOperationRepository;
import com.kloia.eventapis.pojos.EventState;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;

import java.util.UUID;

/**
 * Created by zeldalozdemir on 24/04/2017.
 */
@Aspect
@Slf4j
public class CommandExecutionInterceptor {

    private KafkaOperationRepository kafkaOperationRepository;
    private OperationContext operationContext;

    public CommandExecutionInterceptor(KafkaOperationRepository kafkaOperationRepository, OperationContext operationContext) {
        this.operationContext = operationContext;
        this.kafkaOperationRepository = kafkaOperationRepository;
    }

    @Before("within(com.kloia.eventapis.api.CommandHandler+ || com.kloia.eventapis.api.EventHandler+) && execution(* execute(..)) && args(object)")
    public void before( Object object) throws Throwable {
        UUID eventId = UUID.randomUUID();
        operationContext.setCommandContext(eventId.toString());
        log.info("before method:"+(object == null ? "" : object.toString()));
    }

    @AfterReturning(value = "within(com.kloia.eventapis.api.CommandHandler+ || com.kloia.eventapis.api.EventHandler+) && execution(* execute(..))",returning="retVal")
    public void afterReturning( Object retVal) throws Throwable {
//        kafkaOperationRepository.updateEvent(operationContext.getContext(),operationContext.clearCommandContext(),event -> event.setEventState(EventState.SUCCEDEED));
        log.info("AfterReturning:"+ (retVal == null ? "" : retVal.toString()));
    }

    @AfterThrowing(value = "within(com.kloia.eventapis.api.CommandHandler+ || com.kloia.eventapis.api.EventHandler+) && execution(* execute(..))", throwing = "e")
    public void afterThrowing( Exception e) throws Throwable {
        log.info("afterThrowing method:"+e);
        kafkaOperationRepository.failOperation(operationContext.getContext(),operationContext.getCommandContext(),event -> event.setEventState(EventState.TXN_FAILED));
    }

/*    @Around(value = " @annotation(org.springframework.kafka.annotation.KafkaListener))")
    public Object aroundListen(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        log.info("Event Here:"+proceedingJoinPoint.getArgs()[0].toString());
        return proceedingJoinPoint.proceed();
    }*/

}