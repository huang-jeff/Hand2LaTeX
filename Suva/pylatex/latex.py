from pylatex import Document, Section, Subsection, Command, Math
from pylatex.utils import italic, NoEscape


def fill_document(doc):

    with doc.create(Section('A simple test')):
        doc.append('Read the txt file to understand how to run ')

        with doc.create(Subsection('A subsection')):
            doc.append(Math(data=['2*3', '=', 6]))


if __name__ == '__main__':
    # Basic document
    doc = Document('basic')
    fill_document(doc)

    doc.generate_pdf(clean_tex=False,compiler ='pdflatex')
    doc.generate_tex()


    tex = doc.dumps()  # The document as string in LaTeX syntax