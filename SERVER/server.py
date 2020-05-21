import wave
import numpy
import math, os, datetime
from flask import Flask, request, redirect, render_template

app = Flask(__name__)

# https://pypi.org/project/python-firebase/
from firebase import firebase
firebase = firebase.FirebaseApplication('https://sound-meter-5fde4.firebaseio.com/', None)


def convert_to_db():
    CHUNK = 1024
    wf = wave.open("./static/uploads/output.wav", 'rb')
    data = wf.readframes(CHUNK)

    x = []
    for i in range(len(data)):
        x += [abs(data[i])]

    x = numpy.asarray(x)
    blockLinearRms = numpy.sqrt(numpy.mean(x**2)) # Linear value between 0 -> 1
    blockLogRms = 20 * math.log10(blockLinearRms) # Decibel (dB value) between 0 dB -> -inf dB

    return blockLogRms

@app.route('/')
@app.route('/index')
def index():
    return render_template("index.html")

@app.route('/uploadfile',methods=['GET', 'POST'])
def uploadfile():
    url = request.form["_audio"]
    print(url)
    # Download and process audio
    os.system("wget -O {}".format(url))
    os.system("ffmpeg -i 1.3gp -ac 2 audio3.wav")
    db_value = convert_to_db()

    # Store data on Firebase
    last_id = firebase.get('/Last-Id/', None)
    new_id = last_id + 1
    firebase.put('/', 'Last-Id', new_id)

    current_time = datetime.datetime.now()
    hour, min = str(current_time.hour), str(current_time.minute)
    json = {"time-stamp": hour+":"+min, "value":db_value}
    firebase.put('/Data/', newid, json)
    
    return str(db_value)

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000, debug=True)
