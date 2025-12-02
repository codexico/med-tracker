FROM node:20-slim

WORKDIR /app

# Install git for devcontainer features if needed
RUN apt-get update && apt-get install -y git && rm -rf /var/lib/apt/lists/*

COPY package.json .

# We don't run npm install here because we want to mount the volume
# and install dependencies inside the container to avoid platform issues

CMD ["npm", "run", "dev", "--", "--host"]
