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

#one notification each 30 seconds
FREQUENCY = 60
VERBOSE = true

URL = URI(ARGV[0])

puts "Application Monitor URL: #{URL}"



loopNum = 0;

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
instanceId = rand*100;
instance2Id = rand*100;
loop do
  begin
    #application 1: simple random data and a text parameter
    event = {};
    event["appId"] = "SimpleApp";
    event["nodeId"] = "SimpleNode";
    event["instanceId"] = instanceId;
    
    
    data = {};
    data["rnd"] = rand()*100;
    if rand(2) == 0
      data["event"] = "UP"
    else
      data["event"] = "DOWN"
    end
    event["data"]=data;
    
    sendData(event,"/event")
    # we distribute requests in tme
    sleep(FREQUENCY/2)

    #application 2: some sinus data with tree information
    event = {}
    event["appId"] = "OtherApp"
    if(rand(2) == 0)
      event["nodeId"]="Frontend"
    else
      event["nodeId"]="Backend"
    end
    event["instanceId"] = instance2Id

    data = {}
    data["load"] = {}
    #cycle every 60 loops (1 hour)
    t = 0.5+Math.sin(Math::PI*loopNum/(60*2))/2
    data["load"]["1m"] = t*100
    data["load"]["5m"] = (t*20).to_i*5
    data["load"]["10m"] = (t*10).to_i*10

    data["ps"] = JSON.parse('[{"val1":3,"val2":4,"val3":5},{"a":2,"b":4,"c":"someString"}]')


    sendData(data,"/event")
    sleep(FREQUENCY/2)

    loopNum += 1
  end
end
