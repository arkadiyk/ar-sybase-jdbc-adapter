require 'rubygems'
require 'bundler'
begin
  Bundler.setup(:default, :development)
rescue Bundler::BundlerError => e
  $stderr.puts e.message
  $stderr.puts "Run `bundle install` to install missing gems"
  exit e.status_code
end
require 'rake'

require 'jeweler'
Jeweler::Tasks.new do |gem|
  # gem is a Gem::Specification... see http://docs.rubygems.org/read/chapter/20 for more options
  gem.name = "ar-sybase-jdbc-adapter"
  gem.homepage = "http://github.com/arkadiyk/ar-sybase-jdbc-adapter"
  gem.license = "MIT"
  gem.summary = %Q{Adds support for limit and offset for Rails 3 and Sybase JDBC driver }
  gem.description = %Q{Adds support for limit and offset for Sybase ASE DB to activerecord-jdbc-adapter for Rails 3}
  gem.email = "arkadiyk@gmail.com"
  gem.authors = ["arkadiy kraportov"]
  gem.files = FileList['lib/**/*.rb', 'lib/**/*.jar']
  # Include your dependencies below. Runtime dependencies are required when using your gem,
  # and development dependencies are only needed for development (ie running rake tasks, tests, etc)
  gem.add_runtime_dependency 'activerecord-jdbc-adapter', '>= 1.1.1'
  # gem.add_runtime_dependency 'activerecord', '>= 3.0.3'
  gem.add_runtime_dependency 'arel', '>= 2.0.7'
  gem.add_runtime_dependency 'jdbc-jtds'
  gem.add_development_dependency 'minitest', '>= 2.0.0'
end
Jeweler::RubygemsDotOrgTasks.new

require 'rake/testtask'
Rake::TestTask.new(:test) do |test|
  test.libs << 'lib' << 'test'
  test.pattern = 'test/**/test_*.rb'
  test.verbose = true
end

require 'rcov/rcovtask'
Rcov::RcovTask.new do |test|
  test.libs << 'test'
  test.pattern = 'test/**/test_*.rb'
  test.verbose = true
end

task :default => :test

require 'rake/rdoctask'
Rake::RDocTask.new do |rdoc|
  version = File.exist?('VERSION') ? File.read('VERSION') : ""

  rdoc.rdoc_dir = 'rdoc'
  rdoc.title = "ar-sybase-jdbc-adapter #{version}"
  rdoc.rdoc_files.include('README*')
  rdoc.rdoc_files.include('lib/**/*.rb')
end
