from flask import Flask, render_template, request
from flask_restful import reqparse, abort, Api, Resource
import scanner
import base64

app = Flask(__name__)
api = Api(app)

@app.route('/')
def hello():
    return render_template('test.html')

# TodoList
# shows a list of all todos, and lets you POST to add new tasks
class Upload(Resource):
    def post(self):
        print(request.headers)
        json_data = request.get_json()
        image = json_data['photo']
        image = base64.b64decode(image)
        latex, pdf = scanner.main(image)
        ret = {'latex': latex, 'pdf': base64.standard_b64encode(pdf).decode()}
        return ret

##
## Actually setup the Api resource routing here
##
api.add_resource(Upload, '/upload')


if __name__ == '__main__':
    app.run(debug=True)