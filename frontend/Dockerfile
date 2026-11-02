FROM node:24-alpine AS builder
WORKDIR /app

COPY package.json pnpm-lock.yaml pnpm-workspace.yaml ./

RUN corepack enable
RUN corepack prepare pnpm@latest --activate

RUN pnpm install --unsafe-perm

COPY . .

RUN pnpm run build
