#!/usr/bin/ruby

# Author: Mario Macias (Barcelona Supercomputing Center). 2014

# This library is free software; you can redistribute it and/or
# modify it under the terms of the GNU Lesser General Public
# License as published by the Free Software Foundation; either
# version 2.1 of the License, or (at your option) any later version.
# This library is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.

# See the GNU Lesser General Public License for more details:

# http://www.gnu.org/licenses/lgpl-2.1.html
require 'json'
require 'uri'
require 'net/http'
require 'time'

#PATCH to avoid problems with non-english locales

ENV["LANG"]="en_US.UTF-8"
ENV["LC_NUMERIC"]="en_US.UTF-8"

#one notification each second
FREQUENCY = 1
VERBOSE = true

URL = URI(ARGV[0])

puts "Application Monitor URL: #{URL}"



loopNum = Random.rand(60)

def sendData(data,path)
  http = Net::HTTP.new(URL.host, URL.port)
  headers = {
      "Content-Type" => "application/json"
  }
  puts JSON.generate(data) if VERBOSE
  begin
    request = Net::HTTP::Post.new(URL.path+path);
    request["Content-Type"] = "application/json"
    request.body = JSON.generate(data) #The data sent must be an array
    response = http.request(request)
    puts "RESPONDED: "
    puts response.body
  rescue URI::InvalidURIError => e
    abort("#{e.message}")
  rescue SystemExit
  puts "exiting..."
  exit
  rescue Exception => e
    $stderr.puts "Error! #{e.message}"
    $stderr.puts e.backtrace
  end
end

loop do
  begin
    #application 2: some sinus data with tree information
    event = {}
    event["appId"] = "SinusApp"
    event["nodeId"]="TheSinusNode"

    data = {}
    data["metric"] = {}
    #cycle every 60 loops (1 hour)
    t = 0.5+Math.sin(Math::PI*loopNum/(60*2))/2
    t *= 10;
    # add +/-5% noise
    t += 10*Random.rand(-0.05..0.05)

    data["metric"] = t

    event["data"] = data;

    sendData(event,"/event")
    sleep(FREQUENCY)

    loopNum += 1
  end
end
