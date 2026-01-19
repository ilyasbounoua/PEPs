// Karma configuration file, see link for more information
// https://karma-runner.github.io/1.0/config/configuration-file.html

module.exports = function (config) {
  
  // 1. Detect if we are running on GitHub Actions
  const isCI = process.env.CI === 'true';

  // 2. Only force the local path if NOT in CI (GitHub Actions)
  if (!isCI) {
     // You can keep your local Brave path here for your own laptop
     process.env.CHROME_BIN = String.raw`C:\Users\Ilyas\AppData\Local\BraveSoftware\Brave-Browser\Application\brave.exe`;
  }

  config.set({
    basePath: '',
    frameworks: ['jasmine', '@angular-devkit/build-angular'],
    plugins: [
      require('karma-jasmine'),
      require('karma-chrome-launcher'),
      require('karma-jasmine-html-reporter'),
      require('karma-coverage'),
      require('@angular-devkit/build-angular/plugins/karma')
    ],
    client: {
      jasmine: {
        // you can add configuration options for Jasmine here
      },
      clearContext: false // leave Jasmine Spec Runner output visible in browser
    },
    jasmineHtmlReporter: {
      suppressAll: true // removes the duplicated traces
    },
    coverageReporter: {
      dir: require('path').join(__dirname, './coverage/pepsfront'),
      subdir: '.',
      reporters: [
        { type: 'html' },
        { type: 'text-summary' },
        { type: 'lcov' } // <--- CRITICAL: Added 'lcov' so SonarCloud can read it!
      ]
    },
    reporters: ['progress', 'kjhtml'],
    port: 9876,
    colors: true,
    logLevel: config.LOG_INFO,
    autoWatch: true,
    
    // 3. Define a custom launcher for GitHub Actions (CI)
    customLaunchers: {
      ChromeHeadlessCI: {
        base: 'ChromeHeadless',
        flags: ['--no-sandbox', '--disable-gpu']
      }
    },
    
    // 4. Choose browser based on environment
    browsers: isCI ? ['ChromeHeadlessCI'] : ['Chrome'],
    
    singleRun: false,
    restartOnFileChange: true
  });
};
