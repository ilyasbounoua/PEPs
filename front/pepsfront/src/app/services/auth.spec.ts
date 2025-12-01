/**
 * @author BOUNOUA Ilyas and VAZEILLE Clément
 * @description This file contains unit tests for the AuthService.
 */
import { TestBed } from '@angular/core/testing';
import { AuthService } from './auth';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';

describe('AuthService', () => {
  let service: AuthService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    });
    service = TestBed.inject(AuthService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should have isAuthenticated signal initially set to false', () => {
    expect(service.isAuthenticated()).toBe(false);
  });

  it('should set isAuthenticated to true on successful login', async () => {
    // This is the SHA-256 hash for "password"
    const correctHash = '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918';
    
    // Create a buffer from the hash
    const buffer = new Uint8Array(correctHash.match(/.{1,2}/g)!.map(byte => parseInt(byte, 16))).buffer;

    spyOn(crypto.subtle, 'digest').and.resolveTo(buffer);

    const result = await service.login('PEPS');

    expect(result.success).toBe(true);
    expect(service.isAuthenticated()).toBe(true);
  });

  it('should not set isAuthenticated to true on failed login', async () => {
    spyOn(crypto.subtle, 'digest').and.resolveTo(new ArrayBuffer(32)); // return a wrong hash
    const result = await service.login('wrongpassword');
    expect(result.success).toBe(false);
    expect(service.isAuthenticated()).toBe(false);
  });

  it('should set isAuthenticated to false on logout', () => {
    service.logout();
    expect(service.isAuthenticated()).toBe(false);
  });
});
