#!/usr/bin/env ruby
require 'webrick'
include WEBrick

config = {}
config.update(:Port => 8080)
config.update(:BindAddress => ARGV[0])
config.update(:DocumentRoot => ARGV[1])
server = HTTPServer.new(config)
['INT', 'TERM'].each {|signal|
  trap(signal) {server.shutdown}
}

server.start
