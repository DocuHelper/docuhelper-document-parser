package org.bmserver.docuhelperdocumentparser.kafka.common

import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.messaging.handler.annotation.Header
import org.springframework.stereotype.Component
import java.lang.reflect.ParameterizedType

@Component
abstract class BaseEventListener<T> : CoroutineScope by CoroutineScope(Dispatchers.IO) {
    @Autowired
    private lateinit var om: ObjectMapper

    fun listen(event: Map<String, Any>, @Header eventType: String) {
        if (getEventClass().simpleName != eventType) return
        launch {
            // Map<String, Any> 으로 받아온 뒤, 제네릭 T 타입으로 변환
            val payload: T = om.convertValue(event, getEventClass())

            handle(payload)
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun getEventClass(): Class<T> {
        val type = (this::class.java.genericSuperclass as ParameterizedType).actualTypeArguments.first()
        return type as Class<T>
    }


    abstract suspend fun handle(event: T)
}
