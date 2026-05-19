FROM ubuntu:22.04

LABEL description="Self-hosted GitHub Actions runner for AI Test Automation Framework"
LABEL maintainer="Mariem Elhouche"

ENV DEBIAN_FRONTEND=noninteractive
ENV RUNNER_VERSION=2.334.0
ENV JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
ENV PATH=$JAVA_HOME/bin:/usr/local/bin:$PATH

RUN apt-get update && apt-get install -y --no-install-recommends \
    curl \
    git \
    jq \
    python3 \
    python3-pip \
    xvfb \
    wget \
    unzip \
    gnupg \
    ca-certificates \
    openjdk-17-jdk \
    && rm -rf /var/lib/apt/lists/*

RUN apt-get update && apt-get install -y --no-install-recommends maven && rm -rf /var/lib/apt/lists/*

RUN curl -fsSL https://dl.google.com/linux/linux_signing_key.pub | gpg --dearmor -o /usr/share/keyrings/google-chrome-keyring.gpg && \
    echo "deb [signed-by=/usr/share/keyrings/google-chrome-keyring.gpg] http://dl.google.com/linux/chrome/deb/ stable main" > /etc/apt/sources.list.d/google-chrome.list && \
    apt-get update && apt-get install -y --no-install-recommends google-chrome-stable && \
    rm -rf /var/lib/apt/lists/*

RUN CHROME_FULL_VER=$(google-chrome --version | grep -oP '\d+\.\d+\.\d+\.\d+') && \
    echo "Chrome version: $CHROME_FULL_VER" && \
    curl -fsSL "https://storage.googleapis.com/chrome-for-testing-public/${CHROME_FULL_VER}/linux64/chromedriver-linux64.zip" -o /tmp/chromedriver.zip && \
    unzip -q /tmp/chromedriver.zip -d /tmp/ && \
    mv /tmp/chromedriver-linux64/chromedriver /usr/local/bin/chromedriver && \
    chmod +x /usr/local/bin/chromedriver && \
    rm -rf /tmp/chromedriver*

RUN useradd -m runner && \
    mkdir -p /home/runner/actions-runner /home/runner/.m2 && \
    cd /home/runner/actions-runner && \
    curl -fsSL "https://github.com/actions/runner/releases/download/v${RUNNER_VERSION}/actions-runner-linux-x64-${RUNNER_VERSION}.tar.gz" -o runner.tar.gz && \
    tar xzf runner.tar.gz && \
    rm runner.tar.gz && \
    ./bin/installdependencies.sh && \
    chown -R runner:runner /home/runner

VOLUME /home/runner/.m2

COPY --chown=runner:runner entrypoint.sh /home/runner/actions-runner/entrypoint.sh
RUN chmod +x /home/runner/actions-runner/entrypoint.sh

USER runner
WORKDIR /home/runner/actions-runner

ENTRYPOINT ["/home/runner/actions-runner/entrypoint.sh"]
