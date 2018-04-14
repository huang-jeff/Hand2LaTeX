python3 -c 'import os, subprocess; os.environ["FLASK_APP"]="app.py"; os.environ["FLASK_DEBUG"]="1"; subprocess.Popen(["flask run --host=0.0.0.0"], shell=True).wait()'
