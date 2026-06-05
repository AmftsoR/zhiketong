export function readStorage(key) {
  const value = localStorage.getItem(key)
  return value ? JSON.parse(value) : null
}

export function writeStorage(key, value) {
  localStorage.setItem(key, JSON.stringify(value))
}

export function removeStorage(key) {
  localStorage.removeItem(key)
}