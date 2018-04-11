from flask import Flask, render_template, request
from flask_restful import reqparse, abort, Api, Resource
import coords
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
        image = request.files['userImage']
        latex = coords.main(image)
        ret = {'image': latex}
        return ret

##
## Actually setup the Api resource routing here
##
api.add_resource(Upload, '/upload')


if __name__ == '__main__':
    app.run(debug=True)