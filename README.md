# async-listener
Consumer of rmq topic to update PS in psc db or in IN psc directory

## Developement

### Distribution history

This ecosystem uses many independant components, some of which live an independant life in distinct repositories.
For each release of `psc-toggle-ids`, [the psc-compoents' distribution  history](https://github.com/ansforge/psc-components/blob/main/DISTRIBUTION.md) 
file will need to be updated with the new version, so that we can keep track of compatible component versions, 
and go back to a previous working distribution if need be.

### Release procedure

#### Prerequisites

docker needs to be available in youy release environment. Under windows,  please use a virtual machine like WSL2 or Virtualbox.

#### Procedure

Whenever a version is ready for release, run the following commands on the `main` branch 
(or on the maintenance branch if we're about to issue a production FIX). This should run on any shell, be it `bash`, `cmd` or if needed `gitbash`.

```bash
src/main/scripts/release.sh
```

This command manages the rabbitMQ test container to ensure that tests are run before releasing, creates the release tags (your input will be asked to chose the release version
and the next development version) and pushes the commits & tag to the git repository.