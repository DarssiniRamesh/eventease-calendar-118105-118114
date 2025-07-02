#!/bin/bash
cd /home/kavia/workspace/code-generation/eventease-calendar-118105-118114/calendar_event_frontend
./gradlew lint
LINT_EXIT_CODE=$?
if [ $LINT_EXIT_CODE -ne 0 ]; then
   exit 1
fi

