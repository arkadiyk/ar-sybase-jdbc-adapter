require 'rubygems'
require 'bundler'



begin
  Bundler.setup(:default, :development)
rescue Bundler::BundlerError => e
  $stderr.puts e.message
  $stderr.puts "Run `bundle install` to install missing gems"
  exit e.status_code
end


require 'minitest/autorun'
require 'fileutils'
require 'arel'


$LOAD_PATH.unshift(File.join(File.dirname(__FILE__), '..', 'lib'))
$LOAD_PATH.unshift(File.dirname(__FILE__))

require 'ar-sybase-jdbc-adapter'
require 'support/fake_record'


Arel::Table.engine = Arel::Sql::Engine.new(FakeRecord::Base.new)

class Object
  def must_be_like other
    gsub(/\s+/, ' ').strip.must_equal other.gsub(/\s+/, ' ').strip
  end
end
