#! /usr/bin/env ruby
# coding: utf-8

require "bundler"
Bundler.require

require "csv"
require "json"
require "optparse"

def api_player?
  return false if @headers.member? :event
  return true if @headers.member? :locationurl
  return true if @headers.member? :refurl
end

def api
  if api_player?
    return "/api/player"
  else
    return "/api/playerevent"
  end
end

opts = {}
OptionParser.new do |o|
  o.banner = "Usage: #{File.basename($0)} INPUT-FILE [OPTIONS]"
  o.on("-a", "--all") { |v| opts[:all] = v  }
  o.on("-o", "--ok-only") { |v| opts[:ok] = v  }
  o.on("-k", "--ko-only") { |v| opts[:ko] = v  }
  o.parse!(ARGV)

  if ARGV.size < 1
    puts o.banner
    exit 1
  end
end

infile = ARGV.first

Url = "http://localhost:8080"

begin
  conn = Faraday.new(:url => Url) do |b|
    b.request  :url_encoded
    # b.response :logger
    # b.response :json, :content_type => /\bjson$/
    # b.adapter  :net_http
    b.adapter Faraday.default_adapter
  end
rescue Faraday::ConnectionFailed => e
  puts "Failed to connect: #{Url}"
  exit 1
end

@headers = {}
File.readlines(open(infile)).first.split("|").each do |c|
# CSV.foreach(open(infile), col_sep: "|").first.each do |c|
  next if c.chomp.size == 0
  @headers[c.downcase.intern] = c
end

# @table = CSV.table(open(infile), headers: :first_row, col_sep: "\t", skip_blanks: true, skip_lines: /^#/)
@table = CSV.table(open(infile), col_sep: "|")

buf = []

@table.each_with_index do |row, index|
  expected = ""
  params = nil

  begin
    res = conn.get do |req|
      req.url api
      expected = row[:_expected] == "OK"

      @headers.each do |k,v|
        next unless row[k]
        # column name starts with underscore will be ignored
        next if v =~ /\A_/

        req.params[v] = row[k]
      end
      params = req.params
    end
  rescue Faraday::ConnectionFailed
    buf << { "error" => "Failed to connect to #{url}" }
    break
  end

  if res.status == 200
    j = JSON.load(res.body)
    j = { "id" => index }.merge(j)
    # j["params"] = params
    j["_expected"] = expected
    j["result"] = (expected == j["valid"] && j["error"].nil?) ? "OK" : "NG"
    buf << j
  else
    # Internal Server Error or something similar
    buf << { "error" => res.body, "id" => index, "params" => params, "_expected" => expected }
  end
end

output = case
when opts[:all]
  JSON.dump(buf)
when opts[:ok]
  JSON.dump(buf.select { |e| e["result"] == "OK" })
else
  JSON.dump(buf.select { |e| e["result"] == "NG" })
end

puts output
