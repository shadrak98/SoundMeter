from flask import Flask, request, redirect, render_template

app = Flask(__name__)

@app.route('/')
@app.route('/index')
def index():
    return render_template("index.html")

@app.route('/uploadfile',methods=['GET', 'POST'])
def uploadfile():
    url = request.form["_audio"]
    print(url)
    # audiofile = wget(url)
    # process audiofile
    # get db_value
    db_value = 35
    '''
    # TODO: Store data on Firebase
    '''
    return db_value

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000, debug=True)
