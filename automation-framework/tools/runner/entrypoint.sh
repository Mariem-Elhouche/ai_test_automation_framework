#!/bin/bash
set -e

REPO_URL="${REPO_URL:-https://github.com/Mariem-Elhouche/ai_test_automation_framework}"
RUNNER_NAME="${RUNNER_NAME:-docker-runner}"

if [ ! -f .credentials ]; then
    if [ -z "$RUNNER_TOKEN" ]; then
        echo "FATAL: RUNNER_TOKEN is required on first run"
        exit 1
    fi
    echo "Configuring runner for $REPO_URL as $RUNNER_NAME..."
    ./config.sh --url "$REPO_URL" --token "$RUNNER_TOKEN" \
        --name "$RUNNER_NAME" --labels self-hosted,linux \
        --unattended
fi

exec ./run.sh
