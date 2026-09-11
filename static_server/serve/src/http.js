export async function readJson(context) {
  try {
    return await context.req.json();
  } catch {
    return null;
  }
}

export function success(data, msg = 'ok') {
  return { code: 0, data, msg };
}

export function failure(code, msg) {
  return { code, data: null, msg };
}
