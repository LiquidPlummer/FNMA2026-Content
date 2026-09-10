FROM node:22-alpine

WORKDIR /usr/src/app

# Dependencies first, on their own layer: source edits then don't
# invalidate the npm install. This ordering IS the cache lesson.
COPY package*.json ./
RUN npm install --omit=dev

COPY . .

ENV PORT=3000
EXPOSE 3000

CMD ["node", "server.js"]