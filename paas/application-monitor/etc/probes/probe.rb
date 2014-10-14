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
require 'optparse'
require 'json'
require 'uri'
require 'net/http'
require 'time'

#PATCH to avoid problems with non-english locales

ENV["LANG"]="en_US.UTF-8"
ENV["LC_NUMERIC"]="en_US.UTF-8"

class ArgumentsInfo

	attr_reader :command, :user, :frequency, :verbose, :options

	def initialize(args)
		#default values
		@frequency = 1
		@verbose = false

		opt_parser = OptionParser.new do |opts|
			opts.banner = "Usage: probe.rb [options] ApplicationID NodeId ServiceURL"
			opts.separator ""
			opts.separator "Specific options: "

			#sampling frequency
			opts.on("-f", "--frequency [sec]", Integer,
					"Specify the sampling frequency in seconds (default: 1)") do |f|
				@frequency = f
			end

			opts.on("-u", "--user [user] ",
					"Filters processes for a given user") do |u|
				@user = u
			end

			opts.on("-c", "--command [regexp]",
					"Filters commands whose absolute path matches a ruby regexp") do |r|
				@command = r
			end

			opts.on("-v", "--verbose",
					"Turns on verbose mode") do
				@verbose = true
			end

			opts.on_tail("-h", "--help", "Shows this message") do
				puts opts
				exit
			end
			@options = opts
		end
		opt_parser.parse!(args)
	end
end


def getIostat()
	iostatlines = `iostat`.split("\n")
	firstHeader = iostatlines.shift.split(" ")
	secondHeader = iostatlines.shift.split(" ")
	metrics = iostatlines.shift.split(" ")
	iostat = {}
	for i in 0...secondHeader.length
		if i%3==0
			entry = {}
			iostat[firstHeader[(i/3).to_i]] = to_number(entry);
		end
		entry[secondHeader[i]] = to_number(metrics[i]);
	end
	return iostat

end

## Converts a string to a float, if it can parse to float. If not, leaves the string as is
def to_number(str)
  begin
    Float(str)
  rescue ArgumentError, TypeError
    str
  end
end

## TODO: remove non-numeric values from here
## If allProcesses == false, returns a document with the info of the first process
## If allProcesses == true, returns an array with all the processes that matches the searching chriteria
def getProcessesInfo(options,allProcesses)
	pslines = `ps auxw`.split("\n")
	headers = []

	pslines.shift.split(" ").each { |header| headers.push header.downcase }
	#headers.each {|h| puts h}

	psinfo = []
	pslines.each do |line|
		processInfo = {}
		i = 0
		line.split(" ").each do |data|
			if (headers[i].nil?)
				processInfo[headers[i-1]] += " " + data
			else
				processInfo[headers[i]]= to_number(data)
				i+=1
			end
		end

		if ( processInfo["pid"].to_s != Process.pid.to_s and  # excludes current process
			(options.user.nil? || options.user == processInfo["user"]) and
			(options.command.nil? || processInfo["command"] =~ /#{options.command}/)
		)
      if(allProcesses)
			  psinfo.push processInfo
      else
        return processInfo
      end

		end
	end
	return psinfo
end

begin
	options = ArgumentsInfo.new(ARGV)
rescue OptionParser::InvalidOption => e
	abort("#{e.message}. Use -h argument for help")
end


URL = URI(ARGV[2])
AppId = ARGV[0]
NodeId = ARGV[1]

if (URL == nil || AppId == nil || NodeId == nil)
	$stderr.puts options.options
	abort
end

puts "Application Monitor URL: #{URL}"

http = Net::HTTP.new(URL.host, URL.port)
headers = {
	"Content-Type" => "application/json"
}

event = {};
event["appId"] = AppId;
event["nodeId"] = NodeId;


loop do
	begin
		#monitoringDocument["time"] = Time.now.utc.iso8601  We will allow the monitor to automatically put timestamp

    data = {};

		begin
      data["ps"] = getProcessesInfo(options,false)
		rescue Exception => e
			$stderr.puts "Warning! 'ps' returned an error: #{e.message}"
		end

		begin
      data["iostat"] = getIostat()
		rescue Exception => e
			$stderr.puts "Warning! 'iostat' returned an error: #{e.message}"
		end

		event["data"] = data;
		puts JSON.pretty_generate(event) if options.verbose

		begin
			request = Net::HTTP::Post.new(URL.path+"/event");

			request["Content-Type"] = "application/json"
			request.body = JSON.generate(event) #The data sent must be an array
			response = http.request(request)
			puts response.body
		rescue URI::InvalidURIError => e
			abort("#{e.message}")
		end
	rescue SystemExit
		puts "exiting..."
		exit
	rescue Exception => e
		$stderr.puts "Error! #{e.message}"
		$stderr.puts e.backtrace
	end

	sleep(options.frequency)

end

#`ps auxw`.split("\n").each do |line|
#   line.split(" ").each do |cosa|
#      print "#{cosa}, "
#   end
#   puts "."
#end

#anyadir: iostat vmstat