import json
import _thread
import logging
import flask
from flask import request
import requests
import os.path
import base64
from email.mime.text import MIMEText

app = flask.Flask(__name__)

CLIENT_ID = ''
CLIENT_SECRET = ''
REFRESH_TOKEN = ''
TOKEN = ''
SCOPE = 'https://www.googleapis.com/auth/gmail.send'
#REDIRECT_URI = 'http://localhost:5000/oauth2callback'
DATA = "No message data"

logging.basicConfig(level=logging.INFO)

def create_message(sender, to, subject, message_text, info):
  message = MIMEText(message_text + '\n' + json.dumps(info))
  message['to'] = to
  message['from'] = sender
  message['subject'] = subject
  return { 'raw': base64.urlsafe_b64encode(message.as_bytes()).decode('ascii') }

def save_to_file(filename, creds):
  creds_file = open(filename, "w")
  creds_file.write(json.dumps(creds))
  creds_file.close()


@app.route('/', methods = ['GET', 'POST'])
def index():
  global DATA
  if request.method == 'POST':
    DATA = request.get_json()
  if 'credentials' not in flask.session:
    try:
      with open("creds.json") as creds_file:
        logging.info("Token from file cred.json")
        flask.session['credentials'] = creds_file.read()
        creds_file.close()
    except IOError:
        print("creds.json doesn't exist")
        return flask.redirect(flask.url_for('refresh_token'))
  credentials = json.loads(flask.session['credentials'])
  if credentials['expires_in'] <= 0:
    logging.warn("Expired Token")
    return flask.redirect(flask.url_for('oauth2callback'))
  else:
    sender=os.getenv('GOOGLE_MAIL_SENDER')
    receiver=os.getenv('GOOGLE_MAIL_RECEIVER')
    subject=os.getenv('GOOGLE_MAIL_SUBJECT')
    message_context=os.getenv('GOOGLE_MAIL_MESSAGE')
    info = DATA

    headers =  {'Authorization': 'Bearer {}'.format(credentials['access_token']), 'Content-Type': 'application/json'}
    req_uri = 'https://www.googleapis.com/gmail/v1/users/me/messages/send'
    msg = create_message(sender, receiver, subject, message_context, info)
    r = requests.post(req_uri, headers=headers, json=msg)
    
    if (r.status_code == 401):
      print("Status Code: " + str(r.status_code))
      return flask.redirect(flask.url_for('refresh_token'))
    DATA = "No message data"
    return r.text


@app.route('/oauth2callback')
def oauth2callback():
  logging.warn('OAUTH')
  if 'code' not in flask.request.args:
    auth_uri = ('https://accounts.google.com/o/oauth2/v2/auth?response_type=code'
                '&client_id={}&redirect_uri={}&scope={}').format(CLIENT_ID, REDIRECT_URI, SCOPE)
    return flask.redirect(auth_uri)
  else:
    auth_code = flask.request.args.get('code')
    data = {'code': auth_code,
            'client_id': CLIENT_ID,
            'client_secret': CLIENT_SECRET,
            'redirect_uri': REDIRECT_URI,
            'grant_type': 'authorization_code'}
    r = requests.post('https://oauth2.googleapis.com/token', data=data)
    flask.session['credentials'] = r.text
    _thread.start_new_thread(save_to_file, ("creds.json", r.json()))
    return flask.redirect(flask.url_for('index'))

@app.route('/refresh_token')
def refresh_token():
  global REFRESH_TOKEN
  global TOKEN
  refresh_token_uri='https://oauth2.googleapis.com/token'
  grant_type='refresh_token'
  access_type='offline'
  if REFRESH_TOKEN == "":
    logging.warn("Refresh token is not set trying to pull from creds.json")
    try:
      with open("creds.json", "r+") as creds_file:
        creds = json.load(creds_file)
        REFRESH_TOKEN=creds['refresh_token']
    except IOError:
      print("creds.json doesn't exist")
  logging.info("Retrieving new Access Token")
  headers =  {'Content-Type': 'application/json'}
  params = dict(refresh_token=REFRESH_TOKEN, client_id=CLIENT_ID, client_secret=CLIENT_SECRET, grant_type=grant_type, access_type=access_type)
  resp = requests.post(refresh_token_uri, headers=headers, params=params)
  access_token = resp.json()['access_token']
  flask.session['credentials']=resp.text
  TOKEN=access_token
  return flask.redirect(flask.url_for('index'))

def load_creds():
  global CLIENT_ID
  global CLIENT_SECRET
  global REDIRECT_URI
  global REFRESH_TOKEN
  
  REFRESH_TOKEN = os.getenv("REFRESH_TOKEN")
  REDIRECT_URI = "http://localhost:5000/"
  try:
    with open("client_secret.json", "r") as client_data:
      client = json.load(client_data)["installed"]
      CLIENT_ID = client["client_id"]
      CLIENT_SECRET = client["client_secret"]
  except IOError:
    logging.info("client-secret.json file is missing")

if __name__ == '__main__':
  import uuid
  app.secret_key = str(uuid.uuid4())
  load_creds()
  app.debug = True
  app.run()
