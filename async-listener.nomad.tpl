job "async-listener" {
  datacenters = ["${datacenter}"]
  type = "service"

  vault {
    policies = ["psc-ecosystem"]
    change_mode = "restart"
  }

  affinity {
    attribute = "$\u007Bnode.class\u007D"
    value = "standard"
  }

  group "async-listener-services" {
    count = "1"

    update {
      max_parallel = 1
      min_healthy_time = "30s"
      progress_deadline = "5m"
      healthy_deadline = "2m"
    }

    network {
      port "http" {
        to = 8080
      }
    }

     task "async-listener" {
      restart {
        attempts = 3
        delay = "60s"
        interval = "1h"
        mode = "fail"
      }
      driver = "docker"
      env {
        JAVA_TOOL_OPTIONS = "-Dspring.config.location=/secrets/application.properties -Xms256m -Xmx512m -XX:+UseG1GC"
      }
      config {
        image = "${artifact.image}:${artifact.tag}"
        ports = ["http"]
      }
      template {
        data = <<EOF
{{ range service "psc-rabbitmq" }}
spring.rabbitmq.host={{ .Address }}
spring.rabbitmq.port={{ .Port }}{{ end }}
spring.rabbitmq.username={{ with secret "psc-ecosystem/rabbitmq" }}{{ .Data.data.user }}
spring.rabbitmq.password={{ .Data.data.password }}{{ end }}
spring.rabbitmq.listener.simple.default-requeue-rejected=false
in.api.url={{ with secret "psc-ecosystem/psc-ws-maj" }}{{ .Data.data.in_rass_url }}{{ end }}
{{ range service "psc-api-maj-v2" }}api.base.url=http://{{ .Address }}:{{ .Port }}/psc-api-maj/api{{ end }}
EOF
        destination = "secrets/application.properties"
      }
      resources {
        cpu = 100
        memory = 640
      }
      service {
        name = "$\u007BNOMAD_JOB_NAME\u007D"
        port = "http"
        check {
          type = "tcp"
          port = "http"
          interval = "30s"
          timeout = "2s"
          failures_before_critical = 5
        }
      }
    }
    task "log-shipper" {
      driver = "docker"
      restart {
        interval = "30m"
        attempts = 5
        delay    = "15s"
        mode     = "delay"
      }
      meta {
        INSTANCE = "$\u007BNOMAD_ALLOC_NAME\u007D"
      }
      template {
        data = <<EOH
LOGSTASH_HOST = {{ range service "logstash" }}{{ .Address }}:{{ .Port }}{{ end }}
ENVIRONMENT = "${datacenter}"
EOH
        destination = "local/file.env"
        env = true
      }
      config {
        image = "${registry_path}/filebeat:7.14.2"
      }
    }
  }
}
