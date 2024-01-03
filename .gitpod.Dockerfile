FROM gitpod/workspace-full

## Install dependencies
RUN sudo apt update && \
    sudo apt install -y curl httpie jq


USER gitpod

RUN bash -c ". /home/gitpod/.sdkman/bin/sdkman-init.sh && \
    sdk install java 21.0.1-ms && \
    sdk default java 21.0.1-ms"
