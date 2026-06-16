FROM node:20-alpine AS build
WORKDIR /app

ARG VITE_API_BASE_URL=http://localhost:8080
ENV VITE_API_BASE_URL=${VITE_API_BASE_URL}

ARG VITE_OIDC_ISSUER=http://localhost:8090/realms/pizzagate
ENV VITE_OIDC_ISSUER=${VITE_OIDC_ISSUER}

COPY package*.json ./
RUN npm ci

COPY . .
RUN npm run build

FROM nginxinc/nginx-unprivileged:1.27-alpine
COPY --from=build /app/dist /usr/share/nginx/html

EXPOSE 8080
USER nginx
CMD ["nginx", "-g", "daemon off;"]
