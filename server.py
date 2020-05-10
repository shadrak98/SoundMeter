from flask import Flask, request, redirect

app = Flask(__name__)

@app.route('/uploadfile',methods=['GET', 'POST'])
def uploadfile():
    print(request.data)
    return "Got link"

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000, debug=True)
