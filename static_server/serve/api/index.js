import app from '../src/index.js';

// Vercel Node.js Web Standard handler requires an object with a fetch method.
export default { fetch: app.fetch };
