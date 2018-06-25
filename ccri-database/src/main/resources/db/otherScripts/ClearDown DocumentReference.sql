delete FROM DocumentReferenceAuthor where DOCUMENT_REFERENCE_ID >0;
delete FROM DocumentReferenceAttachment where DOCUMENT_REFERENCE_ID>0;
delete FROM DocumentReferenceIdentifier where DOCUMENT_REFERENCE_ID>0;
delete FROM DocumentReference where DOCUMENT_REFERENCE_ID>0;