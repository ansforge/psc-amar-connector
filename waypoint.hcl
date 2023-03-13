project = "prosanteconnect/${workspace.name}/async-listener"

# Labels can be specified for organizational purposes.
labels = { "domaine" = "psc" }

runner {
    enabled = true
    profile = "secpsc-${workspace.name}"
    data_source "git" {
        url = "https://github.com/prosanteconnect/async-listener.git"
        ref = "${workspace.name}"
    }
    poll {
        enabled = false
    }
}

# An application to deploy.
app "prosanteconnect/async-listener" {
  # the Build step is required and specifies how an application image should be built and published. In this case,
  # we use docker-pull, we simply pull an image as is.
  build {
    use "docker" {
      build_args = {"PROSANTECONNECT_PACKAGE_GITHUB_TOKEN"="${var.github_token}"}
    }
    # Uncomment below to use a remote docker registry to push your built images.
    registry {
      use "docker" {
        image = "${var.registry_username}/async-listener"
        tag   = gitrefpretty()
        username = var.registry_username
        password = var.registry_password
      }
    }
  }

  # Deploy to Nomad
  deploy {
    use "nomad-jobspec" {
      jobspec = templatefile("${path.app}/async-listener.nomad.tpl", {
        datacenter = var.datacenter
        registry_path = var.registry_username
        nomad_namespace = var.nomad_namespace
      })
    }
  }
}

variable "datacenter" {
  type = string
  default = ""
  env = ["NOMAD_DATACENTER"]
}

variable "nomad_namespace" {
  type = string
  default = ""
  env = ["NOMAD_NAMESPACE"]
}

variable "registry_username" {
  type    = string
  default = ""
  env     = ["REGISTRY_USERNAME"]
  sensitive = true
}

variable "registry_password" {
  type    = string
  default = ""
  env     = ["REGISTRY_PASSWORD"]
  sensitive = true
}

variable "github_token" {
  type    = string
  default = ""
  env     = ["PROSANTECONNECT_PACKAGE_GITHUB_TOKEN"]
  sensitive = true
}

variable "registry_path" {
  type = string
  default = "registry.repo.proxy-dev-forge.asip.hst.fluxus.net/prosanteconnect"
}