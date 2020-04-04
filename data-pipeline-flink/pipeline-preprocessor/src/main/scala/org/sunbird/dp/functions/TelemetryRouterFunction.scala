package org.sunbird.dp.functions

import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.streaming.api.functions.ProcessFunction
import org.apache.flink.util.Collector
import org.slf4j.LoggerFactory
import org.sunbird.dp.domain.Event
import org.sunbird.dp.task.PipelinePreprocessorConfig

class TelemetryRouterFunction(config: PipelinePreprocessorConfig)
                             (implicit val eventTypeInfo: TypeInformation[Event]) extends ProcessFunction[Event, Event] {
  private[this] val logger = LoggerFactory.getLogger(classOf[TelemetryRouterFunction])
  override def processElement(event: Event,
                              ctx: ProcessFunction[Event, Event]#Context,
                              out: Collector[Event]): Unit = {
    event.eid().toUpperCase() match {
      case "AUDIT" => ctx.output(config.auditRouteEventsOutputTag, event)
      case "SHARE" => ctx.output(config.shareRouteEventsOutputTag, event)
      case _ => if (config.secondaryRouteEids.contains(event.eid())) {
        ctx.output(config.secondaryRouteEventsOutputTag, event)
      } else {
        ctx.output(config.primaryRouteEventsOutputTag, event)
      }
    }
  }
}
