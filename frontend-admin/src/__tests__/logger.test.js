import { describe, it, expect, vi, beforeEach } from 'vitest'
import logger from '../utils/logger'

describe('Logger', () => {
  beforeEach(() => {
    vi.spyOn(console, 'debug').mockImplementation(() => {})
    vi.spyOn(console, 'info').mockImplementation(() => {})
    vi.spyOn(console, 'warn').mockImplementation(() => {})
    vi.spyOn(console, 'error').mockImplementation(() => {})
  })

  it('should log debug messages', () => {
    logger.debug('Test debug message')
    expect(console.debug).toHaveBeenCalled()
  })

  it('should log info messages', () => {
    logger.info('Test info message')
    expect(console.info).toHaveBeenCalled()
  })

  it('should log warn messages', () => {
    logger.warn('Test warn message')
    expect(console.warn).toHaveBeenCalled()
  })

  it('should log error messages', () => {
    logger.error('Test error message')
    expect(console.error).toHaveBeenCalled()
  })

  it('should log with data', () => {
    logger.info('Test message', { key: 'value' })
    expect(console.info).toHaveBeenCalled()
  })

  it('should log user actions', () => {
    logger.action('Click button', { buttonId: 'submit' })
    expect(console.info).toHaveBeenCalled()
  })

  it('should log API requests', () => {
    logger.api('GET', '/api/users')
    expect(console.debug).toHaveBeenCalled()
  })

  it('should log API responses', () => {
    logger.apiResponse('GET', '/api/users', 200, 100)
    expect(console.debug).toHaveBeenCalled()
  })

  it('should log page views', () => {
    logger.pageView('/dashboard', 'Dashboard')
    expect(console.info).toHaveBeenCalled()
  })

  it('should log performance metrics', () => {
    logger.performance('Component render', 50)
    expect(console.debug).toHaveBeenCalled()
  })
})
