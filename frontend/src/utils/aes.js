import CryptoJS from 'crypto-js';

const AES_KEY = CryptoJS.enc.Utf8.parse('superSecretPassword123');

export function encryptId(id) {
  return CryptoJS.AES.encrypt(
    id.toString(),
    AES_KEY,
    { mode: CryptoJS.mode.ECB, padding: CryptoJS.pad.Pkcs7 }
  ).toString();
} 