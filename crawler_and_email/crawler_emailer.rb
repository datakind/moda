# contact us if you have problem
# vivian.stanford@gmail.com and britt.s.hogue@gmail.com

# The program is written in Ruby. 
# The program allows us to track the NYC Data website 
# for new versions of city property/address data.

require 'nokogiri'
require 'open-uri'
require 'pry'
require 'mail'

# crawl version number for pluto and pad data
main_page1 = Nokogiri::HTML(open('http://www.nyc.gov/html/dcp/html/bytes/dwn_pluto_mappluto.shtml'))
pluto_ver = main_page1.css('body table tr td tr td tr td tr td tr td.plain_text tr td.plain_text strong').children[2].text

main_page2 = Nokogiri::HTML(open('http://www.nyc.gov/html/dcp/html/bytes/dwnpad.shtml'))
pad_ver =main_page2.css('.Link_Blue_11').children[0].text

article_itself= "latest version of pluto is "+pluto_ver+"  ;"+"latest version of pad is "+pad_ver+ "."

# send email to alert me of new version
# need to set up database to track version 
text= 'Pluto/PAD/Mellisa data update newsletter'

mail_options = { :address => "gmail-smtp-in.l.google.com", #if you are not using gmail server, change this
                 :port => 25 }
Mail.defaults do
  delivery_method :smtp, mail_options
end

Mail.deliver do
  to 'vivian.stanford@gmail.com'  #change it to your receiver email
  from 'vivian.zhang@supstat.com' #change it to your sender email
  subject "#{text}"

  text_part do
    body "this email is not supported in plain text"
  end

  html_part do
    content_type 'text/html; charset=UTF-8'
    body "#{article_itself}"
  end
end

