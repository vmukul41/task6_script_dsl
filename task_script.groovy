job('job1(dev_push_git)'){
  
 
  scm {
        github('vmukul41/task3devops')
    }
   triggers {
        scm('* * * * *')
    }
  
    steps {
        shell('''
if cd /root/ | grep task6 
then                 
echo "already present"
sudo cp -rvf * /root/task6                 
else
sudo mkdir /root/task6 
sudo cp -rvf * /root/task6
fi
''')
    }
}

job('job2(lauch_resp_con)'){
  
  

triggers {
        upstream('job1(dev_push_git)', 'SUCCESS')
    }
steps {
     shell('''
cd /root/task6/
if sudo kubectl get deploy | grep deploy
then
echo "already running"
else
if ls | grep *.html
then
echo "it's a html webpage"
sudo kubectl create -f /html-deploy.yml
else 
echo "not html"
if ls | grep *.php
then
echo "it's a php webpage"
sudo kubectl create -f /php-deploy.yml
else
echo "webpage is diffeernt from php"
fi
fi
fi
''')  
        
  }


}

job('job3(testing_mailing)'){

 

 triggers {
        upstream('job2(lauch_resp_con)', 'SUCCESS')
    }
 steps{
    shell('''
podname=$(kubectl get pod  -l app=phpwebsite  --output=jsonpath="{.items[0]..metadata.name }" )

kubectl cp /root/task6/*.php  $podname:/var/www/html/

sleep 5 
x=$(curl -o /dev/null -s -w "%{http_code}" http://192.168.99.127:31546/mycode.php)

if [ $x == 200 ]
then
exit 0
else
exit 1
fi
''')
   }

publishers {
        extendedEmail {
            recipientList('vmukul41@gmail.com')
            
            defaultContent('mail from job')
            contentType('default')
	    attachBuildLog(attachBuildLog = true)
            triggers {
                
                always {
                    
                    content('mail from jenkins')
                    sendTo {
                        developers()
                        recipientList()
                           }
                        }
                      }
                     }
             }


}



