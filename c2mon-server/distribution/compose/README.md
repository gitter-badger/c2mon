# C2MON Deployment via Docker Compose / Kubernetes

This module and its sub-module support a "Docker Compose"-first type of assembly and deployment.

The Dockerfile hierarchy works out of the box with Compose, but Maven can be used to handle release, tune parameters and customize images for alternate platforms (e.g. ARM deployment using different base images and variants).

For reference, consult https://github.com/spotify/dockerfile-maven