cookbook_file "/etc/apt/sources.list" do
	source "sources.list"
	action :create
end


execute "update" do 
	command "apt-get update"
end

package 'apache2'
package 'openjdk-7-jre-headless'
package 'tomcat7'
package 'mysql-server'
package 'phpmyadmin' do
	response_file "phpmyadmin.seed"
end
package 'libapache2-mod-jk'
package 'libopencv2.4-java'
package 'libcudart5.5'
package 'libnppc5.5'
package 'libnppi5.5'
package 'libnpps5.5'
package 'libcufft5.5'
package 'imagemagick'
package 'poppler-utils'

service "mysql" do
  action [:start, :enable]
end

cookbook_file "/tmp/init_mysql.sql" do
  source "init_mysql.sql"
  action :create
end

execute "mysql-init" do 
	command "mysql -u root < /tmp/init_mysql.sql"
	returns [0,1]
end

directory "/opt/forgery/uploadedFiles" do
  owner 'root'
  group 'root'
  mode '0777'
  recursive true
  action :create
end



cookbook_file "/etc/apache2/sites-available/forgery.conf" do
  source "forgery.conf"
  action :create
end

cookbook_file "/usr/share/tomcat7/bin/catalina.sh" do
  source "catalina.sh"
  action :create
end

execute "a2ensite-forgery" do
	command "a2ensite forgery"
end

execute "a2enmod-jk" do
	command "a2enmod jk"
end

execute "a2enmod ssl" do
	command "a2enmod ssl"
end

remote_directory "/etc/apache2/ssl" do
	source "ssl"
end

cookbook_file "/etc/apache2/workers.properties" do
	source "workers.properties"
	action :create
end

cookbook_file "/etc/tomcat7/server.xml" do
	source "server.xml"
	action :create
end

cookbook_file "/etc/apache2/mods-available/jk.conf" do
	source "jk.conf"
	action :create
end

cookbook_file "/var/lib/tomcat7/configuration.xml" do
	source "configuration.xml"
	action :create
end

cookbook_file "/var/lib/tomcat7/webapps/ForgeryWeb.war" do
  source "ForgeryWeb.war"
  action :create
end

remote_directory "/opt/forgery/client" do
	source "ForgeryClient"
end



service 'apache2' do
  action [:restart, :enable]
end

service 'tomcat7' do
  action [:restart, :enable]
end

link "/opt/forgery/client/linux-x86-64/libopencv_java249.so" do
	to "/var/lib/tomcat7/webapps/ForgeryWeb/WEB-INF/lib/libopencv_java249.so"
end

cookbook_file "/etc/init.d/forgery-client" do
	source "client.sh"
	mode "0744"
	action :create
end

execute "create forgery service" do
	command "update-rc.d forgery-client defaults"
end

service 'forgery-client' do
  action [:restart, :enable]
end
