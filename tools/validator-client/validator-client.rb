#! /usr/bin/env ruby

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

if ARGV.size < 1
  puts "Usage: #$0 INPUT-FILE"
  exit 1
end

infile = ARGV.first

url = "http://localhost:8080"

begin
  conn = Faraday.new(:url => url) do |b|
    b.request  :url_encoded
    # b.response :logger
    # b.response :json, :content_type => /\bjson$/
    # b.adapter  :net_http
    b.adapter Faraday.default_adapter
  end
rescue Faraday::ConnectionFailed => e
  puts "Failed to connect: #{url}"
  exit 1
end

@headers = {}
CSV.foreach(open(infile), col_sep: "\t").first.each do |c|
  @headers[c.downcase.intern] = c
end

@table = CSV.table(open(infile), headers: :first_row, converters: :integer, col_sep: "\t", skip_blanks: true, skip_lines: /^#/)

results = []

@table.each_with_index do |row, index|
  expected = ""
  params = nil

  begin
    res = conn.get do |req|
      req.url api
      expected = row[:_expected]

      @headers.each do |k,v|
        next unless row[k]
        next if v =~ /\A_/

        req.params[v] = row[k]
      end
      params = req.params
    end
  rescue Faraday::ConnectionFailed
    results << { "error" => "Failed to connect to #{url}" }
    break
  end

  if res.status == 200
    j = JSON.load(res.body)
    j["id"] = index
    j["params"] = params
    j["_expected"] = expected
    # this is not supported yet
    # j["result"] = expected == j["actual"]
    results << j
  else
    # Internal Server Error or something similar
    results << { "error" => res.body, "id" => index, "params" => params, "_expected" => expected }
  end
end

puts JSON.dump(results)
