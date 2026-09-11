import {
  createHash,
  randomBytes,
  randomUUID,
  scrypt as scryptCallback,
  timingSafeEqual,
} from 'node:crypto';
import { promisify } from 'node:util';

const scrypt = promisify(scryptCallback);

export class AuthService {
  #store;
  #tokenTtlMs;

  constructor({ store, tokenTtlMs }) {
    this.#store = store;
    this.#tokenTtlMs = tokenTtlMs;
  }

  async register({ username, password }) {
    const normalizedUsername = normalizeUsername(username);
    if (this.#store.findUserByNormalizedUsername(normalizedUsername)) {
      throw authError('USERNAME_EXISTS', '用户名已存在。');
    }

    const passwordSalt = randomBytes(16).toString('hex');
    const passwordHash = await hashPassword(password, passwordSalt);
    const now = new Date().toISOString();
    const user = {
      id: randomUUID(),
      username: username.trim(),
      usernameNormalized: normalizedUsername,
      passwordHash,
      passwordSalt,
      createdAt: now,
      updatedAt: now,
    };

    try {
      this.#store.createUser(user);
    } catch (error) {
      if (error?.message?.includes('UNIQUE constraint failed')) {
        throw authError('USERNAME_EXISTS', '用户名已存在。');
      }
      throw error;
    }
    return this.#issueSession(user);
  }

  async login({ username, password }) {
    const user = this.#store.findUserByNormalizedUsername(normalizeUsername(username));
    if (!user || !(await verifyPassword(password, user.passwordSalt, user.passwordHash))) {
      throw authError('INVALID_CREDENTIALS', '用户名或密码错误。');
    }
    return this.#issueSession(user);
  }

  authenticate(token) {
    if (!token) {
      return null;
    }
    const user = this.#store.findUserByTokenHash(hashToken(token), new Date().toISOString());
    return user ? publicUser(user) : null;
  }

  #issueSession(user) {
    const token = randomBytes(32).toString('base64url');
    const now = new Date();
    const expiresAt = new Date(now.getTime() + this.#tokenTtlMs);
    this.#store.createToken({
      tokenHash: hashToken(token),
      userId: user.id,
      createdAt: now.toISOString(),
      expiresAt: expiresAt.toISOString(),
      lastUsedAt: now.toISOString(),
    });
    return {
      token,
      expiresAt: expiresAt.toISOString(),
      user: publicUser(user),
    };
  }
}

function normalizeUsername(username) {
  return username.trim().toLocaleLowerCase('zh-CN');
}

async function hashPassword(password, salt) {
  const key = await scrypt(password, salt, 64);
  return Buffer.from(key).toString('hex');
}

async function verifyPassword(password, salt, expectedHash) {
  const actual = Buffer.from(await hashPassword(password, salt), 'hex');
  const expected = Buffer.from(expectedHash, 'hex');
  return actual.length === expected.length && timingSafeEqual(actual, expected);
}

function hashToken(token) {
  return createHash('sha256').update(token).digest('hex');
}

function publicUser(user) {
  return {
    id: user.id,
    username: user.username,
  };
}

function authError(code, message) {
  const error = new Error(message);
  error.code = code;
  return error;
}
